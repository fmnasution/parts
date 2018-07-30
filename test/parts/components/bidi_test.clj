(ns parts.components.bidi-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [com.stuartsierra.component :as c]
   [parts.components.bidi :as pcb]))

(deftest ring-router-component
  (testing "ring router lifecycle"
    (let [started (-> {:routes    ["" {"/" ::index}]
                       :resources {::index identity}}
                      (pcb/make-ring-router)
                      (c/start))
          stopped (c/stop started)]
      (is (fn? (:handler started)))
      (is (nil? (:handler stopped)))))
  (testing "able to route request"
    (let [started (-> {:routes    ["" {"/a" ::a-index
                                       "/"  ::index
                                       true ::default-index}]
                       :resources
                       {::a-index       #(assoc % :context :a-index)
                        ::index         #(assoc % :context :index)
                        ::default-index #(assoc % :context :default-index)}}
                      (pcb/make-ring-router)
                      (c/start))]
      (are [context uri] (= context (:context ((:handler started) {:uri uri})))
        :index         "/"
        :a-index       "/a"
        :default-index "/foobar")
      (c/stop started))))
