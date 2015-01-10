(ns tcp)
(require '[clojure.math.numeric-tower :as math])
(defn dump-packet [& headers]
  (doseq [header headers]
    (println (Integer/toBinaryString header))
      )

  )
(defn make-offset [word-offset bit-offset size] {:word-offset word-offset :bit-offset bit-offset :size size})
(def header-template 
    ; header is represented as a map of flags, each with a word offset, byte offset (into the word) and a size in bits
    {
      :source-port (make-offset 0 0 16)
      :destination-port (make-offset 0 16 16)
      :seq-num (make-offset 1 0 32)
      :ack-num (make-offset 2 0 32)
      :data-offset (make-offset 3 0 4)
      ; there's a gap here for reserved space
      :flags (make-offset 3 10 6) 
      :window (make-offset 3 16 16)
      :checksum (make-offset 4 0 16)
      :urg (make-offset 4 16 16)
      :options (make-offset 5 0 32)
    }
    ; data goes here after word 5
)

(def blank-header (vec (repeat 10 0)))

(defn make-bitmask [size offset]
  "Generate an inverted bitmask with 'size zeros offset by 'offset ones"
   (-> (math/expt 2 size) 
    (- 1) ; 2^n - 1 e.g. 1111...size
    (bit-shift-left offset) ; 2^n - 1 << offset e.g. 2r111...size 000...offset
    (bit-not) ; invert it
  )
)

(defn set-header-value [header flag value]
  "Arguments: 
    - a symbol from tcp/header-template specifying to value to set (e.g., :ack-num, :urg etc)
    - a value to which to set the symbol in question
    - a vector of at least 6 numbers representing the header
  Returns:
    - A vector representing the new header with the specified value set
  "
  ; pull apart the header template to get the word offset and byte offset for the flag in question
  (let [
          {word-offset :word-offset bit-offset :bit-offset size :size} 
          (tcp/header-template flag)
        ]
      (if (not (contains? header-template flag)) (throw (Exception. (str "Unknown flag: " flag))))
     (vec 
      (map-indexed 
        #(if (= %1 word-offset)
          ; true
          (bit-or
            (bit-and %2 (make-bitmask size bit-offset)) ; first clear the flag...
            (bit-shift-left value bit-offset) ; ...then set it
          )
          ; false
          %2
        ) 
        header
      )
    )
  )
)

(def control-bits {:urg 0 :ack 1 :psh 2 :rst 3 :syn 4 :fin 5})
(defn set-control-bit [control-val flag set-val] 
  (if-not (contains? control-bits flag) (throw (Exception. (str "Flag not found: " flag))))
  (def bitfn (if set-val bit-set bit-clear))
  (bitfn control-val (control-bits flag))
)



(def default-data-offset (* 4 (+ 10 5))) ; 5 word IP header + 10 word TCP header.  I think.
(def blank-flag 0)
(defn syn-packet-header [source-port destination-port init-seq-num]
  (-> blank-header
      (set-header-value :source-port source-port)
      (set-header-value :destination-port destination-port)
      (set-header-value :flags (set-control-bit blank-flag :syn 1))
      (set-header-value :data-offset default-data-offset)
      (set-header-value :window 0x4470) ; default size from here http://technet.microsoft.com/en-us/library/cc938219.aspx
      (set-header-value :seq-num init-seq-num)
  )

)

(defn sum-segments [words] 
  (reduce #(+ %1 (+ (bit-and 0xFFFF %2) (bit-shift-right %2 16))) 0 words) 
)

(defn tcp-checksum [header src-ip dest-ip data-length]
  "Compute the TCP checksum.  See http://www.tcpipguide.com/free/t_TCPChecksumCalculationandtheTCPPseudoHeader-2.htm#Table_158 and http://www.docjar.org/html/api/com/act365/net/SocketUtils.java.html"
  (let [pseudo-header-length 12 ; bytes
        header-length 24 ; bytes
        protocol 6 ; TCP protocol is always 6
        tcp-segment-length (-> header-length (+ pseudo-header-length) (+ data-length))
        pseudo-header [src-ip dest-ip protocol tcp-segment-length]

    ]
    (def s1 (sum-segments (concat pseudo-header header) )) ; sum 16 bit segments
    (def s2 (+ (unsigned-bit-shift-right s1 16) (bit-and s1 0xFFFF))) ; sum halves
    (def s3 (+ s2 (unsigned-bit-shift-right s2 16))) ; add carry
    (bit-and 0xFFFF (bit-not s3)) ; 16 bit ones complement

  )
)

(defn set-checksum [header source-ip dest-ip data-length]
  (set-header-value header :checksum (tcp-checksum header source-ip dest-ip data-length))
  )

(defn ip-to-long [v]
  (reduce #(+ %2 (bit-shift-left %1 8)) 0 v)

  )

(defn inet-to-long [inet]
  (ip-to-long (map (partial bit-and 0xFF) (vec (.getAddress inet)))) ; the bit-and is required to do an unsigned cast from byte to int, otherwise they are autocast to signed ints
  )

(defn get-ip-by-name [name]

  (inet-to-long 
    (first (filter #(= (class %1) java.net.Inet4Address) (vec (java.net.Inet4Address/getAllByName name))))

  )
)

(defn ubyte [val]
  (if (>= val 128)
  (byte (- val 256))
  (byte val)))

(defn ubyte-at [val offset]
  (-> val (bit-shift-right (* 8 offset)) (bit-and 0xFF) (ubyte))
)

(defn header-to-byte-array [header]
  (let [b (byte-array 24)]
    (doseq [i (range 6)]
      (aset-byte b (* 4 i) (ubyte-at (header i) 3))
      (aset-byte b (+ (* 4 i) 1) (ubyte-at (header i) 2))
      (aset-byte b (+ (* 4 i) 2) (ubyte-at (header i) 1))
      (aset-byte b (+ (* 4 i) 3) (ubyte-at (header i) 0))
    )
    b
  )

)

