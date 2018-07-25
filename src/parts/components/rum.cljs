(ns parts.components.rum
  (:require
   [goog.dom :as gdom]
   [cljs.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [rum.core :as r]
   [parts.components.util :as cu]))

;; ---- rum element spec ----

(s/def ::id
  cu/nblank-str?)

(s/def ::constructor
  fn?)

(s/def ::rum-element-params
  (s/keys :req-un [::id ::constructor]))

;; ---- rum element ----

(defrecord RumElement [id constructor node]
  c/Lifecycle
  (start [{:keys [id constructor node] :as this}]
    (s/assert ::rum-element-params this)
    (if (some? node)
      this
      (let [node (gdom/getRequiredElement id)]
        (rum/mount (constructor this) node)
        (assoc this :node node))))
  (stop [{:keys [node] :as this}]
    (if (nil? node)
      this
      (do (r/unmount node)
          (assoc this :node nil)))))

(defn make-rum-element
  [{:keys [id constructor]}]
  (map->RumElement {:id          id
                    :constructor constructor}))
