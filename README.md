# Loire à velo
Ce parcours part de la gare des Aubrais pour aller jusqu'à l'Atlantique.

Les fichiers sources GPX proviennent généralement de eurovelo, et sont par tronçon.
Ils sont rassemblés en les classant dans un dossier par jour de parcours : `jour${numero_du_jour}`.
Les sous-dossiers sont ignorés.

Un script `buildAll.groovy` permet de fusionner tous les fichiers sources pour générer un fichier `.gpx` par journée.
Les fichiers générs sont placés dans `generated`
Usage : `groovy buildAll.groovy`

## Etapes
* Jour 1 : 96km
* Jour 2 : 52km
* Jour 3 : 101km
* Jour 4 : 87km
* Jour 5 : 88km
* Jour 6 : 86km

## Sources
* La route du vélo, fournit des GPX officiels pour chaque tronçon : https://en.eurovelo6-france.com/troncons/orleans-tours
* Geovelo pour faire un trajet personnalisé : https://www.geovelo.fr
* Movescount pour tester un gpx rapidement, connaître sa distance totale et pour ajouter l'altitude à un GPX : http://www.movescount.com/
* Umap pour représenter l'ensemble du parcours : http://umap.openstreetmap.fr/fr/map/la-loire-a-velo_156247

En revanche google est à proscrire : il n'exporte pas en GPX.
