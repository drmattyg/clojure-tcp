(load "tcp")
(require '[clojure.test :as test])
(test/testing "Header template"
  (test/is (= (count tcp/header-template) 10))
  (test/is (contains? tcp/header-template :seq-num))
)
(test/testing "Header generation"
  (let [offset 7 size 13 blank-header (vec (repeat 10 0))]
    (test/is (= (tcp/make-bitmask 0 0) -1 ))
    (test/is 
      (= 
        (Integer/toBinaryString (tcp/make-bitmask size 0))
        (java.lang.Long/toBinaryString 2r11111111111111111110000000000000)
      )
    )
    (test/is 
      (= 
        (Integer/toBinaryString (tcp/make-bitmask size offset))
        (java.lang.Long/toBinaryString 2r11111111111100000000000001111111)
      )
    )
    (test/is
      (=
        (tcp/set-header-value blank-header :flags 2r101101)
        [0 0 0 2r1011010000000000 0 0 0 0 0 0]
      )
    )
    (test/is
      (=
        (tcp/set-header-value blank-header :seq-num 51976)
        [0 51976 0 0 0 0 0 0 0 0]
      )
    )
    (test/is
      (=
        (-> blank-header (tcp/set-header-value :source-port 8080)
          (tcp/set-header-value :destination-port 443))
        [(bit-or 8080 (bit-shift-left 443 16)) 0 0 0 0 0 0 0 0 0]
      )
    )
  )
  (println "Done")
)