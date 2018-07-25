(ns parts.components.util
  (:require
   [clojure.string :as string]
   #?@(:clj  [[clojure.spec.alpha :as s]]
       :cljs [[cljs.spec.alpha :as s]])))

(declare nblank-str?)

;; ---- spec ----

(s/def ::inner-routes
  (s/map-of (s/or :path          nblank-str?
                  :parameterized (s/+ (s/or :path  nblank-str?
                                            :param keyword?))
                  :match-all     true?)
            (s/or :handler       keyword?
                  :inner-routes  ::inner-routes)))

(s/def ::routes
  (s/tuple #(= "" %) ::inner-routes))

(s/def ::resources
  (s/map-of keyword? fn?))

;; ---- helper ----

(defn nblank-str?
  [v]
  (and (string? v) (not (string/blank? v))))
