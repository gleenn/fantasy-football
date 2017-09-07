(ns sportball.routes.home
  (:require [sportball.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [response content-type]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [hiccup.core :as html]))

(defn home-page []
  (layout/render "home.html"))

(defn render-text [text]
  (content-type (response text) "text/plain"))

(defn logged-in? [request]
  (-> request :session :ring.middleware.oauth2/access-tokens :yahoo :token))

(defn fantasy-page [request]
  (let [yh-token (-> request :session :ring.middleware.oauth2/access-tokens :yahoo)]
    (html/html [:div [:h1 "Fantasy Football"]])))

(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/fantasy" [request] (fantasy-page request))
           (GET "/docs" []
             (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                 (response/header "Content-Type" "text/plain; charset=utf-8"))))

