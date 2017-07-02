import groovy.util.XmlSlurper

String guessNamespace(def parsedXml) {
    String namespace = 'http://www.topografix.com/GPX/1/0'
    if (parsedXml.declareNamespace(x: 'http://www.topografix.com/GPX/1/1').'x:trk'.'x:trkseg'.'x:trkpt'.size()) {
        namespace = 'http://www.topografix.com/GPX/1/1'
    }
    return namespace
}

def getPoints(File file, def parsedXml, String namespace) {
    def result = parsedXml.declareNamespace(x: namespace)
            .'x:trk'.'x:trkseg'.'x:trkpt'
    
    return result
            
}

int copyFile(def out, def file) {
    out.println "<!-- $file -->"
    int totalPoint = 0
    def parsedXml = new XmlSlurper().parseText(file.text)
    
    String namespace = guessNamespace(parsedXml)
    def parsedXmlWithNs = parsedXml.declareNamespace(x: namespace)
    
    int trackCount = parsedXmlWithNs.'x:trk'.'x:trkseg'.size()
    if (trackCount != 1) {
        throw new RuntimeException("Found $trackCount tracks. One expected")
    }
    
    String copy = ''
    getPoints(file, parsedXml, namespace).eachWithIndex { point, index ->
    
        if (file.name.contains('62_Champtoceaux_MauvessurLoire_parcours')) {
            // We need to inverse the order of the points in some files
            copy = """<trkpt lat="${point.'@lat'}" lon="${point.'@lon'}"><ele>0</ele></trkpt>""" + copy
        } else {
            copy += """<trkpt lat="${point.'@lat'}" lon="${point.'@lon'}"><ele>0</ele></trkpt>"""
        }
        totalPoint = index
    }
    
    out.println copy
    
    return totalPoint
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

        println "${outputFileName} :"
        files.sort { it.name }.each {
            print "\t\tCopie de ${it.name}"
            println " (${copyFile(out, it)} points)"
        }


        out.println """\t</trkseg>
\t</trk>
</gpx>
"""

    }
}

mergeTracks(1, 'jour1_42-46_Aubrais-Onzain-via-Chambord')
mergeTracks(2, 'jour2_47-49_Onzain-Tours')
mergeTracks(3, 'jour3_50-53_Tours-Brossay-via-Saumur')
mergeTracks(4, 'jour4_54-57-Brossay-Becon')
mergeTracks(5, 'jour5_60-63-Becon-Nantes')
mergeTracks(6, 'jour6_64-66-Nantes-StNazaire-viaPontStNazaire')

'FIN' 
