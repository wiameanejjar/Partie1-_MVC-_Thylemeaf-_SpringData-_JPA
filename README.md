# Rapport de TP – Application de Gestion Hospitalière avec Spring Boot, Spring Security et Thymeleaf

## 📌 Objectif du TP

L'objectif de ce TP est de développer une application web complète de gestion hospitalière en utilisant Spring Boot comme framework principal, Spring MVC pour l'architecture web,
Thymeleaf comme moteur de templates,Spring Data JPA pour la persistance des données,Spring Security pour la gestion de l'authentification et des autorisations.
Cette application permet de modéliser les principales entités d’un système hospitalier, telles que les patients, les médecins, les rendez-vous, les consultations, et leur gestion au travers de services et d’un contrôleur REST.

L'application doit permettre de :
 - Gérer les patients (CRUD complet).
 - Implémenter une pagination des résultats.
 - Ajouter des fonctionnalités de recherche.
 - Sécuriser l'accès aux différentes fonctionnalités.
 - Utiliser un système de templates pour une interface cohérente.
 - Valider les données des formulaires.

---

## 🧱 Structure du Projet

Le projet suit une architecture MVC (Modèle-Vue-Contrôleur) typique d'une application Spring Boot, il contient les packages suivants :
 - entities : contient les classes de domaine représentant les entités métier : Classe Patient.
 - repositories : contient les interfaces JPA permettant l’accès aux données :
    - Interface PatientRepository: Fournit des méthodes CRUD automatiques et la recherche paginée.
 - security :Gère l'authentification et l'autorisation via Spring Security, incluant la modélisation des utilisateurs/rôles, la configuration de sécurité et les contrôleurs dédiés.
   Il contient les packages:
    - Entités qui contient les classes AppRole pour définir les rôles d'accès et AppUser pour modéliser un utilisateur avec ses credentials et rôles associés.
    - Répo qui contient les interfaces AppRoleRepository / AppUserRepository pour persister et rechercher rôles/utilisateurs en base.
    - Service qui contient l'interface AccountService qui définit les contrats pour la gestion des utilisateurs et rôles, l'implémentation AccountServiceImpl qui implémente les règles métier (validation des mots de passe, gestion des transactions avec @Transactional), ainsi l'implémentation UserDetailServiceImpl pour adapter le modèle AppUser à Spring Security en implémentant UserDetailsService pour l'authentification.
    - La classe SecurityConfig pour configurer les règles d'accès et l'authentification (ex: routes protégées).
 - web : Contient les contrôleurs MVC :
     - Classe PatientController: Gère l'affichage et la recherche des patients.
     - Classe SecurityController: Gère les vues liées à l'authentification.
 - HospitalApplication : Point d'entrée de l'application avec configuration automatique.
 - templates: Contient les vues Thymeleaf pour l'interface utilisateur, structurées avec des fragments réutilisables et des formulaires liés aux entités.Il contient les fichiers suivants:
     - template1.html : Template de base avec navbar et layout commun à toutes les pages.
     - patients.html : Affiche la liste paginée des patients avec recherche et actions (éditer/supprimer).
     - formPatients.html : Formulaire de création d'un patient avec validation.
     - editPatients.html : Vue spécifique pour modifier un patient existant.
     - login.html : Page d'authentification avec formulaire de connexion.
     - notAuthorized.html : Message d'erreur pour les accès non autorisés.
  - application.properties : Paramètres de l'application (BDD, sécurité, etc.).
  - schema.sql : Script SQL pour initialiser la structure de la base de données.
  ![img](hospital.JPG)  
  
## 📄 Explication détaillée des Classes
### 1. Classe Patient :
La classe Patient représente une entité JPA correspondant à la table des patients dans la base de données. Elle est annotée avec @Entity, ce qui indique à JPA qu’il s’agit d’une entité persistante. L’utilisation de Lombok avec @Data, @NoArgsConstructor, et @AllArgsConstructor permet de générer automatiquement les méthodes usuelles (getters, setters, constructeurs, etc.).
 - Attributs principaux :
   - id : clé primaire de type Long, générée automatiquement (@GeneratedValue).
   - nom : nom du patient (type String).
   - dateNaissance : date de naissance annotée avec @Temporal(TemporalType.DATE) pour indiquer qu’il s’agit d’un champ de type date sans l’heure.
   - malade : booléen indiquant si le patient est actuellement malade.
   - rendezVous : collection de rendez-vous associés au patient, avec une relation @OneToMany. Le champ mappedBy = "patient" signifie que la relation est gérée par l'entité RendezVous, et fetch = FetchType.LAZY optimise la performance en chargeant la collection uniquement sur demande.

  ![img](patient.JPG)
### 2. Classe Medecin :
La classe Medecin est également une entité JPA représentant les médecins dans le système. Elle utilise les mêmes annotations Lombok que les autres entités pour générer du code standard.
 - Attributs principaux :
    - id : identifiant unique généré automatiquement.
    - nom, email, specialite : informations personnelles du médecin.
    - rendezVous : liste des rendez-vous du médecin avec une relation @OneToMany(mappedBy = "medecin"). Annotée avec @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) pour empêcher son affichage lors des sérialisations JSON, évitant ainsi les cycles infinis et protégeant la structure de données.

  ![img](medecin.JPG)
### 3. Classe RendezVous :
Cette classe modélise les rendez-vous entre les patients et les médecins. Elle est reliée à plusieurs entités par des relations @ManyToOne et @OneToOne.
 - Attributs principaux :
   - id : identifiant du rendez-vous (type String).
   - date : date du rendez-vous.
   - status : statut du rendez-vous défini par une énumération StatusRDV, persisté sous forme de chaîne (@Enumerated(EnumType.STRING)).
   - patient : relation @ManyToOne avec Patient, annotée avec @JsonProperty(WRITE_ONLY) pour éviter la récursivité lors de la sérialisation.
   - medecin : relation @ManyToOne avec Medecin.
   - consultation : relation @OneToOne(mappedBy = "rendezVous"), indiquant qu’un rendez-vous peut être associé à une seule consultation.

  ![img](rendezVous.JPG)
### 4. Classe Consultation :
La classe Consultation représente les consultations médicales ayant lieu suite à un rendez-vous.
 - Attributs principaux :
    - id : identifiant généré automatiquement.
    - dateConsulation : date à laquelle la consultation a eu lieu.
    - rapport : rapport écrit du médecin suite à la consultation.
    - rendezVous : relation @OneToOne avec l’entité RendezVous, indiquant l’unicité du lien entre une consultation et son rendez-vous. L’annotation @JsonProperty(WRITE_ONLY) permet d’éviter que la consultation ne soit exposée avec toutes les informations du rendez-vous en JSON.

  ![img](consultation.JPG)
### 5. Enumération StatusRDV :
Cette énumération représente les différents statuts possibles d’un rendez-vous :
   - PENDING : en attente.
   - CANCELLED : annulé.
   - DONE : terminé.

Elle est utilisée dans l’entité RendezVous pour gérer l’état d’un rendez-vous via l’annotation @Enumerated(EnumType.STRING), ce qui permet de stocker le nom du statut (et non sa position) dans la base de données.

  ![img](statusRDV.JPG)

 ## 🗂️ Repositories
### - Interface `ConsultationRepository` : 
L’interface ConsultationRepository est une interface de persistance dédiée à l’entité Consultation. Elle hérite de JpaRepository<Consultation, Long>, ce qui lui permet d’accéder automatiquement à un ensemble complet de méthodes CRUD (Create, Read, Update, Delete) sans avoir à écrire du code supplémentaire. Le type Consultation représente l’entité gérée, tandis que Long est le type de sa clé primaire (id).  
Grâce à Spring Data JPA, cette interface est détectée automatiquement et injectée dans les services via l’injection de dépendances.
 ![img](consultationrepository.JPG)
### - Interface `MedecinRepository` :  
L’interface MedecinRepository est conçue pour interagir avec la base de données à travers l’entité Medecin. Elle étend JpaRepository<Medecin, Long>, ce qui lui donne accès à toutes les opérations CRUD standards. Elle introduit également une méthode personnalisée findByNom(String nom) permettant de rechercher un médecin en fonction de son nom.  
Spring Data JPA se base sur le nom de la méthode pour générer automatiquement son implémentation, évitant ainsi d’écrire une requête SQL manuelle.
 ![img](medecinrepository.JPG)
 ### - Interface `PatientRepository` : 
L’interface PatientRepository assure l’accès aux données de l’entité Patient en héritant de JpaRepository<Patient, Long>. Comme les autres interfaces, elle bénéficie des méthodes de base pour la manipulation des entités en base de données. Elle déclare aussi une méthode personnalisée findByNom(String name) permettant de récupérer un patient à partir de son nom. Cette méthode est automatiquement interprétée par Spring pour générer une requête correspondante.
 ![img](patientrepository.JPG)
 ### - Interface `RendezVousRepository` : 
RendezVousRepository est une interface de gestion de la persistance des entités RendezVous. Elle hérite de JpaRepository<RendezVous, String>, ce qui signifie que l’identifiant principal de l’entité est une String. Elle permet d'effectuer facilement toutes les opérations de base sur les rendez-vous sans devoir implémenter les requêtes manuellement.
 ![img](RDVrepository.JPG)
## 🛠️ Services
### -  Interface `IHospitalService`:
L’interface IHospitalService définit les opérations métier principales liées à la gestion des entités médicales telles que les patients, les médecins, les rendez-vous et les consultations. Elle joue un rôle essentiel dans l’architecture de l’application en assurant une séparation claire entre la couche contrôleur (qui traite les requêtes HTTP) et la couche de persistance (repositories).  
Cette interface facilite l’évolutivité, la maintenance et les tests unitaires du système en fournissant une abstraction des traitements métiers.
 - Voici les méthodes déclarées dans l’interface IHospitalService :
    - savePatient(Patient patient) : enregistre un nouveau patient dans la base de données.
    - saveMedecin(Medecin medecin) : ajoute un médecin au système.
    - saveRDV(RendezVous rendezVous) : crée un rendez-vous médical. Un identifiant unique est généré automatiquement.
    - saveConsultation(Consultation consultation) : enregistre une consultation médicale.

Cette interface pose les fondations de la logique métier, laissant l’implémentation concrète aux classes de service.
 ![img](ihospitalservice.JPG)
 ### -  Implémentation `HospitalServiceImpl`:
La classe HospitalServiceImpl est l’implémentation concrète de l’interface IHospitalService. Annotée avec @Service, elle est détectée automatiquement par le framework Spring comme un composant métier injectable. L’annotation @Transactional garantit que chaque opération métier est exécutée dans une transaction cohérente, ce qui protège l’intégrité des données même en cas d’erreur.  
Les dépendances nécessaires (PatientRepository, MedecinRepository, RendezVousRepository, ConsultationRepository) sont injectées via un constructeur, pratique rendue possible par Spring, évitant ainsi l’usage direct de @Autowired.
 - Voici les principales méthodes de cette classe :
     - savePatient(Patient patient) : délègue l'enregistrement d’un patient au PatientRepository.
     - saveMedecin(Medecin medecin) : enregistre un nouveau médecin via le MedecinRepository.
     - saveRDV(RendezVous rendezVous) : génère un identifiant aléatoire (UUID) pour chaque rendez-vous avant de l’enregistrer.
     - saveConsultation(Consultation consultation) : persiste une nouvelle consultation médicale dans la base de données.

HospitalServiceImpl centralise ainsi toute la logique métier liée à la gestion des entités médicales, tout en s’appuyant sur les repositories pour la persistance. Elle constitue un exemple typique de couche service dans une application Spring Boot bien structurée.
 ![img](impl1.JPG)
 ![img](impl2.JPG)
## 🌐 Web:
###  - Classe `PatientRestController`:
La classe PatientRestController est un contrôleur REST qui expose les données relatives aux patients via des endpoints HTTP. Grâce à l’annotation @RestController, Spring reconnaît automatiquement cette classe comme un composant dédié à la gestion des requêtes web.  

Elle utilise @Autowired pour injecter automatiquement une instance de PatientRepository, qui assure les opérations de persistance sur l'entité Patient.
La méthode patientList(), annotée avec @GetMapping("/patients"), est déclenchée lorsqu'une requête HTTP GET est envoyée à l'URL /patients. Cette méthode interroge la base de données via patientRepository.findAll() pour récupérer la liste complète des patients, et retourne le résultat sous forme de JSON.  

Ce contrôleur joue ainsi un rôle essentiel dans l’architecture REST de l’application, en faisant le lien entre les clients (navigateur, front-end, etc.) et la base de données via le repository.

![Texte alternatif](web.JPG) 

## Classe Principale `HospitalApplication`:
La classe HospitalApplication constitue le point d’entrée de l’application Spring Boot de gestion hospitalière. Annotée avec @SpringBootApplication, elle active la configuration automatique de Spring ainsi que le scan des composants, ce qui permet de démarrer l'application de manière autonome.  

La méthode main() utilise SpringApplication.run() pour lancer l’application. Une méthode start() annotée avec @Bean retourne un CommandLineRunner, permettant d’exécuter automatiquement un ensemble d’instructions à l’initialisation de l’application.  
 - Voici ce que cette méthode réalise étape par étape :
    - Création de plusieurs patients à l’aide de la méthode savePatient() du service métier IHospitalService. Les données sont générées dynamiquement à partir d’une liste de prénoms.
    - Création de plusieurs médecins, chacun avec un nom, un e-mail, et une spécialité (aléatoirement "Cardio" ou "Dentiste"), via la méthode saveMedecin().
    - Récupération d’un patient et d’un médecin existants à partir de la base (par id ou nom) pour leur affecter un rendez-vous.
    - Création et enregistrement d’un rendez-vous (RendezVous) entre le patient et le médecin, avec un statut PENDING et une date courante.
    - Enfin, création d’une consultation (Consultation) liée au rendez-vous précédemment enregistré, avec un rapport médical fictif.

Ce bloc d’initialisation est très utile pour simuler un scénario clinique complet dès le lancement, ce qui facilite le test, la démonstration, et la validation fonctionnelle de l'application.
  ![Texte alternatif](host1.JPG) 
  ![Texte alternatif](host2.JPG) 

## ⚙️ Configuration (`application.properties`):
Ce fichier contient les paramètres essentiels de configuration de l’application Spring Boot, en particulier pour la gestion du port d'accès, la base de données et la console H2. Voici une explication détaillée des principales propriétés utilisées :
  - spring.application.name=Hospital : définit le nom de l’application Spring Boot comme “Hospital”.
  - spring.datasource.url=jdbc:h2:mem:hospital : configure la connexion à une base de données H2 en mémoire, nommée ici "hospital".
  - spring.h2.console.enabled=true : active la console web H2, accessible dans le navigateur à l’adresse http://localhost:8086/h2-console. Cette interface permet de visualiser et d’interroger la base de données H2 pendant l'exécution.
  - server.port=8086 : spécifie que l'application sera accessible sur le port 8086 (au lieu de la valeur par défaut 8080). Cela peut être utile pour éviter les conflits de port avec d'autres services.
    ![Texte alternatif](h2.JPG)
- Résultat Attendu
Au lancement de l’application :
     - Plusieurs patients (Mohamed, Hassan, Wiame) et médecins (aymane, Hanane, yasmine) sont créés automatiquement avec des données simulées.
     - Des rendez-vous sont générés entre certains patients et médecins avec un statut initial PENDING.
     - Une consultation est enregistrée pour le premier rendez-vous, avec un rapport médical.
     - Toutes les entités (Patient, Medecin, RendezVous, Consultation) sont persistées automatiquement en base H2.

Il est possible de visualiser les tables et le contenu des enregistrements via la console H2, accessible à l’adresse :
 -  http://localhost:8086/h2-console  
en utilisant le JDBC URL suivant :
  - jdbc:h2:mem:hospital
    
## Problème rencontré: 
je voulais également vous informer que j’ai rencontré un problème sur mon PC concernant l’utilisation de XAMPP/WampServer. J’ai essayé à plusieurs reprises de le résoudre, mais je n’ai pas encore trouvé de solution définitive. Toutefois, je continue à chercher activement une solution afin de pouvoir avancer sur les prochains projets.


 ## - Conclusion
Ce TP m’a permis de mettre en œuvre une application Spring Boot complète de gestion hospitalière, avec intégration de plusieurs entités métier liées entre elles (Patient, Medecin, RendezVous, Consultation).
Grâce à Spring Data JPA, l’initialisation des données, et la console H2, j’ai pu tester le cycle complet de création, persistance et consultation des enregistrements.
Il renforce aussi la compréhension des relations entre entités, de l’utilisation des repositories JPA, et de la configuration d’une base de données embarquée pour un développement efficace.

Auteur :  
Anejjar Wiame


 
  
