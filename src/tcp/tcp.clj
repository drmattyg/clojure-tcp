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

(def control-bits {:urg 1 :ack 2 :psh 3 :rst 4 :syn 5 :fin 6})
(defn set-control-bit [control-val flag set-val] 
  (if-not (contains? control-bits flag) (throw (Exception. (str "Flag not found: " flag))))
  (def bitfn (if set-val bit-set bit-clear))
  (bitfn control-val (control-bits flag))
)
