(set-env!
 :source-paths #{"src/" "test/"}
 :resource-paths #{"resources/"}
 :dependencies '[;; ---- clj ----
                 [org.clojure/clojure "1.10.0-alpha5"]
                 [metosin/ring-http-response "0.9.0"]
                 [http-kit "2.3.0" :scope "provided"]
                 [com.datomic/datomic-free "0.9.5697" :scope "provided"]
                 [io.rkn/conformity "0.5.1" :scope "provided"]
                 [org.clojure/tools.namespace "0.3.0-alpha4"]
                 ;; ---- cljc ----
                 [org.clojure/core.async "0.4.474" :scope "provided"]
                 [com.stuartsierra/component "0.3.2"]
                 [datascript "0.16.6" :scope "provided"]
                 [datascript-schema "0.2.1" :scope "provided"]
                 [bidi "2.1.3" :scope "provided"]
                 [rum "0.11.2" :scope "provided"]
                 [cljs-ajax "0.7.4" :scope "provided"]
                 [com.taoensso/sente "1.12.0" :scope "provided"]
                 ;; ---- cljs ----
                 [org.clojure/clojurescript "1.10.339" :scope "provided"]
                 ;; ---- dev ----
                 [org.clojure/tools.reader "1.3.0" :scope "test"]
                 [samestep/boot-refresh "0.1.0" :scope "test"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]
                 [metosin/bat-test "0.4.0"]])

(require
 '[samestep.boot-refresh :refer [refresh]]
 '[adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot push-release]]
 '[metosin.bat-test :refer [bat-test]])

(def +version+
  "0.1.7-SNAPSHOT")

(bootlaces! +version+)

(task-options!
 push {:ensure-branch nil
       :repo-map      {:checksum :warn}}
 pom  {:project     'parts
       :version     +version+
       :description "Collection of reusable components for my personal use"
       :url         "http://github.com/fmnasution/parts"
       :scm         {:url "http://github.com/fmnasution/parts"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask dev-repl
  []
  (comp
   (repl :server true)
   (watch)
   (refresh)
   (bat-test)))
