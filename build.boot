(set-env!
 :source-paths #{"src/"}
 :dependencies '[;; ---- clj ----
                 [org.clojure/clojure "1.10.0-alpha5"]
                 [metosin/ring-http-response "0.9.0"]
                 [http-kit "2.3.0"]
                 ;; ---- cljc ----
                 [org.clojure/core.async "0.4.474"]
                 [com.stuartsierra/component "0.3.2"]
                 [bidi "2.1.3"]
                 [rum "0.11.2"]
                 [cljs-ajax "0.7.4"]
                 [com.taoensso/sente "1.12.0"]
                 ;; ---- cljs ----
                 [org.clojure/clojurescript "1.10.339"]
                 ;; ---- dev ----
                 [samestep/boot-refresh "0.1.0" :scope "test"]])

(require
 '[samestep.boot-refresh :refer [refresh]])
