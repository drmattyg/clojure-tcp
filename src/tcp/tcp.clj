(ns tcp)
(defn dump-packet [& headers]
  (doseq [header headers]
    (println (Integer/toBinaryString header))
      )

  )

(def header-template ; header is represented as a vector of 32 bit words
  ; each value in each hash is an offset into the word for that value
  ; e.g. :flags is offset 10 bits into the 4th word
  [
    {
      :source-port 0
      :destination-port 16
    }
    {
      :seq-num 0 
    }; using an arbitrary fixed initial sequence number for simplicity in this case
    {
      :ack-num 0
    }
    {
      :data-offset 0
      :flags 10
      :window 16
    }
    {
      :checksum 0
      :urg 16
    }
    {
      :options 0
    }
    ; data goes here
  ]
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
          [word-offset byte-offset]
          (->> tcp/header-template 
            (map-indexed vector) 
            (filter #(contains? (% 1) flag))
            (first)
          )
        ]
     
  )
)