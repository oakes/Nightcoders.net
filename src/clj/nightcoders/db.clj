(ns nightcoders.db
  (:require [clojure.java.jdbc :as jdbc])
  (:import [org.h2.tools Server]))

(def ^:const db-spec
  {:classname "org.h2.Driver"
   :subprotocol "h2"
   :subname "file:./main"
   :user "sa"
   :password ""})

(defn start-ui []
  (.start (Server/createWebServer (into-array String ["-webPort" "8082"]))))

(defn create-tables []
  (try
    (jdbc/db-do-commands db-spec
      (jdbc/create-table-ddl :users
        [[:id :identity] [:email :varchar]]
        {:conditional? true}))
    (jdbc/db-do-commands db-spec
      (jdbc/create-table-ddl :projects
        [[:id :identity] [:user_id :int]]
        {:conditional? true}))
    (catch Exception _)))

(defn select-user [db-conn email]
  (jdbc/query db-conn ["SELECT * FROM users WHERE email = ?" email]))

(defn select-user! [email]
  (jdbc/with-db-connection [db-conn db-spec]
    (select-user db-conn email)))

(defn insert-user [db-conn email]
  (if-let [user (first (select-user db-conn email))]
    (:id user)
    (-> (jdbc/insert! db-conn :users {:id nil :email email})
        first
        vals
        first)))

(defn insert-user! [email]
  (jdbc/with-db-connection [db-conn db-spec]
    (insert-user db-conn email)))

(def ^:const limit 300)

(defn select-projects [db-conn page]
  (jdbc/query db-conn [(str "SELECT * FROM projects ORDER BY id DESC LIMIT " limit
                         " OFFSET " (* limit page))]))

(defn select-projects! [page]
  (jdbc/with-db-connection [db-conn db-spec]
    (select-projects db-conn page)))

(defn insert-project [db-conn user-id]
  (-> (jdbc/insert! db-conn :projects {:id nil :user_id user-id})
      first
      vals
      first))

(defn insert-project! [user-id]
  (jdbc/with-db-connection [db-conn db-spec]
    (insert-project db-conn user-id)))

