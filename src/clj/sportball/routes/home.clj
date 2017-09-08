(ns sportball.routes.home
  (:require [sportball.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [response content-type]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [hiccup.core :as html]
            [hiccup.page :as page]
            [hiccup.util :refer [escape-html]]
            [clj-http.client :as http]
            [ring.middleware.oauth2 :as oauth2]
    #_[clojure.xml :as xml]
    #_[clojure.data.zip :as zip]))

(defn home-page []
  (layout/render "home.html"))

(defn logged-in? [request]
  (-> request :session :ring.middleware.oauth2/access-tokens :yahoo :token))

(defn make-yahoo-request [request url])

(defn fantasy-page [request]
  (let [yh-token (-> request :session :ring.middleware.oauth2/access-tokens :yahoo :token)
        request-url (str "https://fantasysports.yahooapis.com/fantasy/v2/users;use_login=1/games;game_keys=nfl/teams")]
    (try
      (let [response-body (:body (http/get request-url {:oauth-token yh-token}))]
        (assoc (response/ok response-body) :headers {"Content-Type" "text/xml"}))
      (catch Exception e
        (response/conflict (str e " - " (.getMessage e)))))))

#_(defn fantasy-page [request]
    (let [yh-token (-> request :session :ring.middleware.oauth2/access-tokens :yahoo :token)
          request-url (str "https://fantasysports.yahooapis.com/fantasy/v2/users;use_login=1/games;game_keys=nfl/teams")
          response (try (http/get request-url {:oauth-token yh-token})
                        (catch Exception e (str e " " (.getMessage e))))]
      (page/html5 [:div "hi"])))

(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/fantasy" [request] fantasy-page)
           (GET "/docs" []
             (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                 (response/header "Content-Type" "text/plain; charset=utf-8"))))

