(ns user
  (:require [mount.core :as mount]
            [sportball.figwheel :refer [start-fw stop-fw cljs]]
            sportball.core))

(defn start []
  (mount/start-without #'sportball.core/repl-server))

(defn stop []
  (mount/stop-except #'sportball.core/repl-server))

(defn restart []
  (stop)
  (start))


