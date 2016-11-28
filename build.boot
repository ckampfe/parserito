(set-env!
 :source-paths    #{"src/cljs" "src/clj"}
 :resource-paths  #{"resources"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.293"]
                 [reagent "0.6.0"]
                 [com.lucasbradstreet/instaparse-cljs "1.4.1.2"]
                 [adzerk/boot-cljs              "1.7.228-2"   :scope "test"]
                 [adzerk/boot-cljs-repl         "0.3.3"       :scope "test"]
                 [adzerk/boot-reload            "0.4.13"      :scope "test"]
                 [pandeiro/boot-http            "0.7.2"       :scope "test"]
                 [com.cemerick/piggieback       "0.2.1"       :scope "test"]
                 [org.clojure/tools.nrepl       "0.2.12"      :scope "test"]
                 [weasel                        "0.7.0"       :scope "test"]
                 [org.martinklepsch/boot-garden "1.3.2-0"     :scope "test"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[org.martinklepsch.boot-garden :refer [garden]])

(deftask build []
  (comp (speak)
        (cljs)
        (garden :styles-var 'parserito.styles/screen
:output-to "css/garden.css")))

(deftask run []
  (comp (serve)
        (watch)
        (cljs-repl)
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced}
                      garden {:pretty-print false})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none :source-map true}
                 reload {:on-jsload 'parserito.app/init})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))


