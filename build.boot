(set-env!
 :source-paths    #{"src/cljs" "src/clj"}
 :resource-paths  #{"resources"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.671"]
                 [reagent "0.7.0"]
                 [instaparse "1.4.7"]
                 [adzerk/boot-cljs              "2.0.0"   :scope "test"]
                 [adzerk/boot-cljs-repl         "0.3.3"   :scope "test"]
                 [adzerk/boot-reload            "0.5.1"   :scope "test"]
                 [pandeiro/boot-http            "0.8.3"   :scope "test"]
                 [com.cemerick/piggieback       "0.2.2"   :scope "test"]
                 [org.clojure/tools.nrepl       "0.2.12"  :scope "test"]
                 [weasel                        "0.7.0"   :scope "test"]
                 [org.martinklepsch/boot-garden "1.3.2-0" :scope "test"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[org.martinklepsch.boot-garden :refer [garden]])

(deftask production []
  (task-options! cljs {:optimizations :advanced}
                 garden {:pretty-print false})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none :source-map true}
                 reload {:on-jsload 'parserito.app/init})
  identity)

(deftask build []
  (comp (notify :audible true)
        (production)
        (cljs)
        (garden :styles-var 'parserito.styles/screen
                :output-to "css/garden.css")
        (target)))

(deftask run []
  (comp (serve)
        (watch)
        (cljs-repl)
        (reload)
        (build)))

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))
