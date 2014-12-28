(ns tcp)
(require '[clojure.math.numeric-tower :as math])
(defn dump-packet [& headers]
  (doseq [header headers]
    (println (Integer/toBinaryString header))
      )

  )
(defn make-offset [word-offset byte-offset size] {:word-offset word-offset :byte-offset byte-offset :size size})
(def header-template ; header is represented as a vector of 32 bit words
  ; each value in each hash is an offset into the word for that value
  ; e.g. :flags is offset 10 bits into the 4th word
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
   (-> (math/expt 2 size) 
    (- 1) ; 2^n - 1
    (bit-shift-left offset)) ; 2^n - 1 << offset
    (bit-not)
  )

(defn set-header-value [flag value header]
  "Arguments: 
    - a symbol from tcp/header-template specifying to value to set (e.g., :ack-num, :urg etc)
    - a value to which to set the symbol in question
    - a vector of at least 6 numbers representing the header
  Returns:
    - A vector representing the new header with the specified value set
  "
  ; pull apart the header template to get the word offset and byte offset for the flag in question
  (let [
          {word-offset :word-offset byte-offset :byte-offset size :size} 
          (tcp/header-template flag)
        ]
     (map-indexed 
        #(if (= %1 word-offset)
          ; true
          (bit-or
            (bit-and %2 (make-bitmask size byte-offset)) ; first clear the flag then set it
            (bit-shift-left %2 offset)
          )
          ; false
          %2
        ) 
        header
    )
  )
)