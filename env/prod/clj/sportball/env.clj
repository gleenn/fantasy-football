(ns sportball.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[sportball started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[sportball has shut down successfully]=-"))
   :middleware identity})
