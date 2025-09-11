(ns org.yulqen.proxypox.core)

(defn -main []
  (println "Hello,world!"))

(defn greet
  "Greet these people"
  [person1 person2]
  (str "Hello, " person1 " and " person2 "!"))

(comment
  (greet "tobby the lar" "sniggers")
  )
