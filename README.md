# Loire à velo
Ce projet présente le parcours à vélo partant de la gare des Aubrais pour aller jusqu'à l'Atlantique. Il permet de représenter [ce parcours dans uMap](http://umap.openstreetmap.fr/fr/map/la-loire-a-velo_156247)

Les fichiers sources GPX proviennent généralement de eurovelo, et sont par petits tronçons.
Ils sont rassemblés en les classant dans un dossier par jour de parcours : `jour${numero_du_jour}`.
Les sous-dossiers sont ignorés.

Un script [`buildAll.groovy`](buildAll.groovy) permet de fusionner tous les fichiers sources pour générer un fichier `.gpx` par journée.
Les fichiers générés sont placés dans [`generated`](generated)

## Usage
### Fusion des GPX
Pour fusionner tous les gpx par journée : 
`groovy buildAll.groovy`

### Déploiement dans uMap
uMap est configuré pour afficher automatiquement la dernière version des GPX sur ce projet github (branche master). Aucune action n'est nécessaire dans uMap pour faire la mise à jour.

En revanche pour ajouter une journée, il faut :
* ajouter un calque dans uMap
* définir la source de donnée de ce calque comme une source distante :
  * données distantes
  * URL : fournir l'URL github du fichier gpx brut correspondant. Ex : ```https://raw.githubusercontent.com/manuqcr/loire_velo/master/generated/jour3_50-53_Tours-Brossay-via-Saumur.gpx```
  * Format : GPX
  * Dynamique : ON (permet de mettre à jour la carte à chaque changement dans github)

## Ressources en ligne
* La route du vélo, fournit des GPX officiels pour chaque tronçon : https://en.eurovelo6-france.com/troncons/orleans-tours
* Geovelo pour faire un trajet personnalisé : https://www.geovelo.fr
* Maplorer fournit un visualiseur de fichiers gpx : http://www.maplorer.com/view_gpx_fr.html (:warning: il semblerait que certains points ne soient pas bien pris en compte par maplorer)
* Movescount pour tester un gpx rapidement, connaître sa distance totale et pour ajouter l'altitude à un GPX : http://www.movescount.com/
* Umap pour représenter l'ensemble du parcours : http://umap.openstreetmap.fr/fr/map/la-loire-a-velo_156247

En revanche google est à proscrire : il n'exporte pas en GPX.
