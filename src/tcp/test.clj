(load "tcp")
(require '[clojure.test :as test])
(test/testing "Header template"
  (test/is (= (count tcp/header-template) 10))
  (test/is (contains? tcp/header-template :seq-num))
)