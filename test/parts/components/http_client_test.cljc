(ns parts.components.http-client-test
  (:require
   [com.stuartsierra.component :as c]
   [org.httpkit.server :refer [run-server]]
   [parts.components.http-client :as pchclt]
   #?@(:clj  [[clojure.test :refer [deftest testing is]]
              [clojure.core.async :as a]]
       :cljs [[cljs.test :refer [deftest testing is]]
              [cljs.core.async :as a]])))

(deftest http-client-component
  (testing "http client lifecycle"
    (let [started (-> {:routes ["" {"/" ::index}]}
                      (pchclt/make-http-client)
                      (c/start))
          stopped (c/stop started)]
      (is (fn? (:caller started)))
      (is (nil? (:caller stopped)))))
  (testing "invoking caller"
    (let [chan    (a/chan)
          server  (run-server (constantly
                               {:status  200
                                :body    "foobar"
                                :headers {"content-type" "text/plain"}})
                              {:port 9090})
          started (-> {:routes ["http://localhost:9090" {"/" ::index}]}
                      (pchclt/make-http-client)
                      (c/start))]
      ((:caller started)
       ::index
       {}
       :get
       {:handler #(a/put! chan %)
        :format  :text})
      (is (= "foobar" (a/<!! chan)))
      (c/stop started)
      (server :timeout 100))))
