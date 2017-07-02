import groovy.util.XmlSlurper


int copyFile(def out, def file) {
    out.println "<!-- $file -->"
    int totalPoint = 0
    def parsedXml = new XmlSlurper().parseText(file.text)
    def namespace = 'http://www.topografix.com/GPX/1/0'
    if (parsedXml.declareNamespace(x: 'http://www.topografix.com/GPX/1/1').'x:trk'.'x:trkseg'.'x:trkpt'.size()) {
        namespace = 'http://www.topografix.com/GPX/1/1'
    }

    parsedXml.declareNamespace(x: namespace)
            .'x:trk'.'x:trkseg'.'x:trkpt'
            .eachWithIndex { point, index ->
        out.println """<trkpt lat="${point.'@lat'}" lon="${point.'@lon'}"><ele>0</ele></trkpt>"""
        totalPoint = index
    }
    totalPoint
}

void mergeTracks(int day, String newTrackName) {
    String rootPath = '.'
    List<File> files = []
    new File("$rootPath/jour${day}").eachFile { File file ->
        if (!file.name.endsWith('.gpx')) {
            return
        }
        files << file
    }

    String outputFileName = "$rootPath/${newTrackName}.gpx"
    new File(outputFileName).withWriter { out ->

        out.println """<?xml version="1.0" encoding="UTF-8"?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1">
\t<trk>
\t\t<name>${newTrackName}</name>
		<trkseg>
"""

        files.sort { it.name }.each {
            print "${outputFileName} << Copie de ${it.name}"
            println " (${copyFile(out, it)} points)"
        }


        out.println """\t\t</trkseg>
\t</trk>
</gpx>
"""

    }
}

mergeTracks(1, 'jour1_42-46_Aubrais-Onzain-via-Chambord')
'FIN' 
