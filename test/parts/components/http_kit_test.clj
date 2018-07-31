(ns parts.components.http-kit-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [com.stuartsierra.component :as c]
   [org.httpkit.client :as hkitclt]
   [parts.components.http-kit :as pchkit]))

(deftest web-server-component
  (testing "web server lifecycle"
    (let [started (-> {:config       {:port 9090}
                       :ring-handler {:handler identity}}
                      (pchkit/make-web-server)
                      (c/start))
          stopped (c/stop started)]
      (is (some? (:server started)))
      (is (nil? (:server stopped)))))
  (testing "handling request"
    (let [started (-> {:config
                       {:port 9090}

                       :ring-handler
                       {:handler (fn [{:keys [uri wrapped?]}]
                                   {:status (if wrapped? 200 404)
                                    :body   uri})}

                       :ring-middleware
                       {:wrapper (fn [handler]
                                   (fn [request]
                                     (handler (assoc request :wrapped? true))))}}
                      (pchkit/make-web-server)
                      (c/start))
          {:keys [status body wrapped?]}
          @(hkitclt/get "http://localhost:9090/foobar" {:as :text})]
      (is (= 200 status))
      (is (= "/foobar" body))
      (c/stop started))))
