(ns parts.components.sente-test
  (:require
   [com.stuartsierra.component :as c]
   [clojure.test :refer [deftest testing is are]]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
   [parts.components.sente :as pcsnt]
   [parts.test-helpers :as pth]))

(deftest websocket-server-component
  (testing "websocket server lifecycle"
    (let [started (-> {:server-adapter (get-sch-adapter)}
                      (pcsnt/make-websocket-server)
                      (c/start))
          stopped (c/stop started)]
      (is (fn? (:ring-ajax-get started)))
      (is (fn? (:ring-ajax-post started)))
      (is (pth/chan? (:recv-chan started)))
      (is (fn? (:send! started)))
      (is (instance? clojure.lang.Atom (:connected-uids started)))
      (are [k] (nil? (k stopped))
        :ring-ajax-get
        :ring-ajax-post
        :recv-chan
        :send!
        :connected-uids))))
