(ns parts.components.datascript-test
  (:require
   [com.stuartsierra.component :as c]
   [datascript.core :as dts]
   [parts.components.datascript :as pcdts]
   #?@(:clj  [[clojure.test :refer [deftest testing is]]]
       :cljs [[cljs.test :refer [deftest testing is]]])))

(deftest datascript-component
  (testing "datascript lifecycle"
    (let [started (c/start (pcdts/make-datascript {:config {}}))
          stopped (c/stop started)]
      (is (dts/conn? (:conn started)))
      (is (nil? (:conn stopped))))))
