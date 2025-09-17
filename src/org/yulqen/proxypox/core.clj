(ns org.yulqen.proxypox.core
  (:require [org.httpkit.client :as http]
            [org.httpkit.server :as server]
            [compojure.core :refer [defroutes GET]] ; Corrected the syntax for Compojure
            [compojure.route :as route]
            [org.yulqen.proxypox.image :as image]
            [org.yulqen.proxypox.utils :as utils]))


(import '[javax.crypto Mac])
(import java.nio.charset.StandardCharsets)

;; This atom will hold the server instance so we can start and stop it.
;; `defonce` ensures it's only defined once, which is good for REPL usage.
(defonce server-instance (atom nil))

;; --- Main application code ---

(defroutes app
  (GET "/" [] "<h1>Hello, world</h1>")
  (GET "/test" [] "<h2>This is a test page</h2>")
  (GET "/image/:b64-url" [b64-url]
    (try
      (let [decoded-url (utils/decode-url b64-url)
            image-bytes (image/just-image decoded-url "https://alphabetlearning.online/static/images/AL_long_logo_black_grey_750.1ec1231fe406.png")]
        (if image-bytes
          {:status 200
           :headers {"Content-Type" "image/png"}
           :body image-bytes}
          {:status 404
           :body "Image not found or could not be processed."}))
      (catch Exception e
        {:status 500
         :body (str "Error processing request: " (.getMessage e))})))
  
  (route/not-found "<h1>Page not found</h1>"))

(defn start-server
  "Starts the HTTP server and stores the instance in the server-instance atom."
  []
  (when-not @server-instance
    (let [s (server/run-server app {:port 9000})]
      (reset! server-instance s)
      (println "Server started on port 9000."))))

(defn stop-server
  "Stop the http server gracefully."
  []
  (when-not (nil? @server-instance)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server-instance :timeout 100)
    (reset! server-instance nil)
    (println "Stopping server.")))


(defn -main []
  (println "Starting the server...")
  (start-server))
