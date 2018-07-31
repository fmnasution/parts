(ns parts.components.middleware-test
  (:require
   [com.stuartsierra.component :as c]
   [parts.components.middleware :as pcmdw]
   #?@(:clj  [[clojure.test :refer [deftest testing is]]]
       :cljs [[cljs.test :refer [deftest testing is]]])))

(defn- pre-middleware
  [f]
  (fn [handler]
    (fn [request]
      (handler (f request)))))

(defn- post-middleware
  [f]
  (fn [handler]
    (fn [request]
      (f (handler request)))))

(deftest middleware-component
  (testing "middleware lifecycle"
    (let [started (-> {:entries [(pre-middleware #(assoc % :a :a))]}
                      (pcmdw/make-middleware)
                      (c/start))
          stopped (c/stop started)]
      (is (fn? (:wrapper started)))
      (is (nil? (:wrapper stopped)))))
  (testing "applying middleware"
    (let [started (-> {:entries [(pre-middleware #(assoc % :a :a))
                                 (pre-middleware #(assoc % :b :b))
                                 (post-middleware #(update % :c inc))]}
                      (pcmdw/make-middleware)
                      (c/start))
          handler ((:wrapper started) #(assoc % :c 1))
          result (handler {:foo :bar})]
      (is (= {:a :a
              :b :b
              :c 2
              :foo :bar} result))
      (c/stop started))))
