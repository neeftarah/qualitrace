```plantuml
@startuml
actor Admin
boundary UI_GammeControle
control ControleurGammeControle
entity Article
entity Specification
control LifecycleSpecification
Admin -> UI_GammeControle : ouvrirFicheArticle(articleId)
UI_GammeControle -> ControleurGammeControle : getArticle(articleId)
ControleurGammeControle -> Article : findById(articleId)
Article --> ControleurGammeControle : article
alt Article inexistant ou archivé
    ControleurGammeControle --> UI_GammeControle : erreur("Article invalide")
    UI_GammeControle --> Admin : affichage erreur
    stop
end
Admin -> UI_GammeControle : saisirSpecification(nom, methode, unite, min, max)
UI_GammeControle -> ControleurGammeControle : validerEtEnregistrer(specDTO)
alt seuil_min >= seuil_max
    ControleurGammeControle --> UI_GammeControle : erreur("Seuil min >= seuil max")
    UI_GammeControle --> Admin : affichage erreur
    stop
end
alt Specification existante
    ControleurGammeControle -> Specification : update(specDTO)
else Nouvelle specification
    ControleurGammeControle -> LifecycleSpecification : create(specDTO)
    LifecycleSpecification --> ControleurGammeControle : specification
end
opt modification impactante (gamme modifiée)
    ControleurGammeControle -> Article : setEtat("BROUILLON")
end
ControleurGammeControle -> LifecycleSpecification : save(specification)
ControleurGammeControle -> ControleurGammeControle : logAction("MAJ gamme de contrôle")
ControleurGammeControle --> UI_GammeControle : succès
UI_GammeControle --> Admin : message succès
@enduml
```
