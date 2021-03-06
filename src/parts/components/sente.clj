(ns parts.components.sente
  (:require
   [clojure.spec.alpha :as s]
   [clojure.core.async :as a]
   [com.stuartsierra.component :as c]
   [taoensso.sente :refer [make-channel-socket!]]
   [taoensso.sente.interfaces :refer [IServerChanAdapter IPacker]]))

;; ---- websocket server spec ----

(s/def ::server-adapter
  #(satisfies? IServerChanAdapter %))

(s/def ::user-id-fn
  fn?)

(s/def ::csrf-token-fn
  fn?)

(s/def ::handshake-data-fn
  fn?)

(s/def ::ws-kalive-ms
  pos-int?)

(s/def ::lp-timeout-ms
  pos-int?)

(s/def ::send-buf-ms-ajax
  pos-int?)

(s/def ::send-buf-ms-ws
  pos-int?)

(s/def ::packer
  (s/or :edn    #(= :edn %)
        :packer #(satisfies? IPacker %)))

(s/def ::server-option
  (s/nilable (s/keys :opt-un [::user-id-fn
                              ::csrf-token-fn
                              ::handshake-data-fn
                              ::ws-kalive-ms
                              ::lp-timeout-ms
                              ::send-buf-ms-ajax
                              ::send-buf-ms-ws
                              ::packer])))

(s/def ::websocket-server-params
  (s/keys :req-un [::server-adapter ::server-option]))

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
    (s/assert ::websocket-server-params this)
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
