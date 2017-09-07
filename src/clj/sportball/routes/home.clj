(ns sportball.routes.home
  (:require [sportball.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [response content-type]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render "home.html"))

(defn render-text [text]
  (content-type (response text) "text/plain"))

(defn logged-in? [request]
  (-> request :session :ring.middleware.oauth2/access-tokens :yahoo :token))

(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/token" [] (fn [request] (str (-> request :session :ring.middleware.oauth2/access-tokens :yahoo))))
           (GET "/docs" []
             (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                 (response/header "Content-Type" "text/plain; charset=utf-8"))))

