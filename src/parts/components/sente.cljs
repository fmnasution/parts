(ns parts.components.sente
  (:require
   [cljs.core.async :as a]
   [com.stuartsierra.component :as c]
   [taoensso.sente :as sente :refer [make-channel-socket!]]))

;; ---- websocket client ----

(defrecord WebsocketClient [server-uri
                            client-option
                            chsk
                            recv-chan
                            send!
                            state]
  c/Lifecycle
  (start [{:keys [recv-chan] :as this}]
    (if (some? recv-chan)
      this
      (let [{:keys [chsk ch-recv send-fn state]}
            (make-channel-socket! server-uri client-option)]
        (assoc this
               :chsk      chsk
               :recv-chan ch-recv
               :send!     send-fn
               :state     state))))
  (stop [{:keys [chsk recv-chan] :as this}]
    (if (nil? recv-chan)
      this
      (do (sente/chsk-disconnect! chsk)
          (a/close! recv-chan)
          (assoc this
                 :chsk      nil
                 :recv-chan nil
                 :send!     nil
                 :state     nil)))))

(defn make-websocket-client
  [{:keys [server-uri client-option]}]
  (map->WebsocketClient {:server-uri    server-uri
                         :client-option client-option}))
