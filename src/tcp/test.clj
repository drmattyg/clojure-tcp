(require '[clojure.math.numeric-tower :as math])
(println "Testing")
(let [size 5 offset 6]
  (println (Integer/toBinaryString (- (math/expt 2 size) 1)))
)