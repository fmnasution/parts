(ns parts.components.util
  (:require
   [clojure.string :as string]
   #?@(:clj  [[clojure.spec.alpha :as s]]
       :cljs [[cljs.spec.alpha :as s]])))

;; ---- spec ----

(s/def ::nblank-str
  #(and (string? %) (not (string/blank? %))))

(s/def ::inner-routes
  (s/map-of (s/or :path          ::nblank-str
                  :parameterized (s/+ (s/or :path  ::nblank-str
                                            :param keyword?))
                  :match-all     true?)
            (s/or :handler       keyword?
                  :inner-routes  ::inner-routes)))

(s/def ::routes
  (s/tuple ::nblank-str ::inner-routes))

(s/def ::resources
  (s/map-of keyword? fn?))
