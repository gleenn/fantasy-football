(ns sportball.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [sportball.layout :refer [error-page]]
            [sportball.routes.home :refer [home-routes]]
            [sportball.routes.services :refer [service-routes]]
            #_[sportball.routes.oauth :refer [oauth-routes]]
            [compojure.route :as route]
            [sportball.env :refer [defaults]]
            [mount.core :as mount]
            [sportball.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    #_#'oauth-routes

    #'service-routes
    (route/not-found
      (:body
        (error-page {:status 404
                     :title  "page not found"})))))

(defn app [] (middleware/wrap-base #'app-routes))
