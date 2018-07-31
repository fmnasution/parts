(ns parts.components.http-client-test
  (:require
   [com.stuartsierra.component :as c]
   [parts.components.http-client :as pchcl]
   #?@(:clj  [[clojure.test :refer [deftest testing is]]]
       :cljs [[cljs.test :refer [deftest testing is]]])))

(deftest http-client-component
  (testing "http client lifecycle"
    (let [started (-> {:routes ["" {"/" ::index}]}
                      (pchcl/make-http-client)
                      (c/start))
          stopped (c/stop started)]
      (is (fn? (:caller started)))
      (is (nil? (:caller stopped)))))
  #_(testing "invoking caller"
      (testing "without middleware"
        (let [started (-> {:routes ["" {"/" ::index}]}
                          (pchcl/make-http-client)
                          (c/start))]
          ))
      (testing "with middleware"
        (let [started (-> {:routes ["" {"/" ::index}]}
                          (pchcl/make-http-client)
                          (assoc :option-middleware
                                 {:wrapper #(assoc % :wrapped? true)})
                          (c/start))]
          ))))
