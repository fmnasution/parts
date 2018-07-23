(ns parts.components.sente
  (:require
   [clojure.core.async :as a]
   [com.stuartsierra.component :as c]
   [taoensso.sente :refer [make-channel-socket!]]))

;; ---- websocket server ----

(defrecord WebsocketServer [server-adapter
                            server-option
                            ring-ajax-get
                            ring-ajax-post
                            recv-chan
                            send!
                            connected-uids]
  c/Lifecycle
  (start [{:keys [server-adapter server-option recv-chan] :as this}]
    (if (some? recv-chan)
      this
      (let [{:keys [ch-recv
                    send-fn
                    connected-uids
                    ajax-post-fn
                    ajax-get-or-ws-handshake-fn]}
            (make-channel-socket! server-adapter server-option)]
        (assoc this
               :ring-ajax-get  ajax-get-or-ws-handshake-fn
               :ring-ajax-post ajax-post-fn
               :recv-chan      ch-recv
               :send!          send-fn
               :connected-uids connected-uids))))
  (stop [{:keys [recv-chan] :as this}]
    (if (nil? recv-chan)
      this
      (do (a/close! recv-chan)
          (assoc this
                 :ring-ajax-get  nil
                 :ring-ajax-post nil
                 :recv-chan      nil
                 :send!          nil
                 :connected-uids nil)))))

(defn make-websocket-server
  [{:keys [server-adapter server-option]}]
  (map->WebsocketServer {:server-adapter server-adapter
                         :server-option  server-option}))
