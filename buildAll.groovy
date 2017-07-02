import groovy.util.XmlSlurper
import groovy.transform.Field

/**
 * We need to inverse the order of the points in some files
 */
@Field
final List<String> filesToReverse = []

/**
 * Detect the namespace of the xml
 * @param parsedXml xml parsed with JSonSlurper
 * @return namespace to use
 */
String guessNamespace(def parsedXml) {
    String namespace = 'http://www.topografix.com/GPX/1/0'
    if (parsedXml.declareNamespace(x: 'http://www.topografix.com/GPX/1/1').'x:trk'.'x:trkseg'.'x:trkpt'.size()) {
        namespace = 'http://www.topografix.com/GPX/1/1'
    }
    return namespace
}

/**
 * Copy the points in the file into out
 * @param out where to copy the points
 * @param file input
 * @return total processed points
 */
int copyPointsInFile(def out, def file) {

    out.println "<!-- $file -->"
    
    def parsedXml = new XmlSlurper().parseText(file.text)

    String namespace = guessNamespace(parsedXml)
    def parsedXmlWithNs = parsedXml.declareNamespace(x: namespace)

    int trackCount = parsedXmlWithNs.'x:trk'.'x:trkseg'.size()
    if (trackCount != 1) {
        throw new RuntimeException("Found $trackCount tracks. One expected")
    }

    boolean reverse = filesToReverse.find { rule -> file.name.contains(rule) }
    
    int totalPoint = 0
    int totalElevationFound = 0
    String copy = ''
    parsedXml.declareNamespace(x: namespace)
            .'x:trk'.'x:trkseg'.'x:trkpt'
            .each { point ->

        String elevation = point.ele.text()
        if (elevation) {
            elevation="<ele>${elevation}</ele>"
            totalElevationFound++
        }
            
        String lineForTrackPoint = """<trkpt lat="${point.'@lat'}" lon="${point.'@lon'}">${elevation}</trkpt>\n"""
        if (reverse) {
            copy = lineForTrackPoint + copy
        } else {
            copy += lineForTrackPoint
        }
        totalPoint++
    }

    out.println copy

    println " (${totalPoint} points${reverse ? ' REVERSED' : ''} and $totalElevationFound elevation)"
    
    return totalPoint
}

/**
 * Write a mergefile, copying all the points of the tracks of the inputFiles
 * @param out the output stream
 * @param newTrackName new name for the merged track
 * @param inputFiles files to copy
 */
void writeFile(def out, String newTrackName, List<File> inputFiles) {
    int totalPoints = 0
    out.println """<?xml version="1.0" encoding="UTF-8"?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1">
\t<trk>
\t\t<name>${newTrackName}</name>
		<trkseg>
"""
        
        inputFiles.sort { it.name }.each {
            print "\tCopie de ${it.name}"
            totalPoints += copyPointsInFile(out, it)
        }

        out.println """\t</trkseg>
\t</trk>
</gpx>
"""

    println "\t** TOTAL : $totalPoints points **\n"
}

/**
 * Merge tracks in relativeSourcePath into a new output file
 * @param relativeSourcePath relative path where all source gpx files are located
 * @param newTrackName new name for the merged track
 */
void mergeTracks(String relativeSourcePath, String newTrackName) {
    String rootPath = '.'
    List<File> files = []
    new File("$rootPath/${relativeSourcePath}").eachFile { File file ->
        if (!file.name.endsWith('.gpx')) {
            return
        }
        files << file
    }

    String outputFileName = "$rootPath/${newTrackName}.gpx"
    new File(outputFileName).withWriter { out ->
        println "${outputFileName} :"
        writeFile(out, outputFileName, files)
    }
}

filesToReverse.add('62_Champtoceaux_MauvessurLoire_parcours')

mergeTracks('jour1', 'jour1_42-46_Aubrais-Onzain-via-Chambord')
mergeTracks('jour2', 'jour2_47-49_Onzain-Tours')
mergeTracks('jour3', 'jour3_50-53_Tours-Brossay-via-Saumur')
mergeTracks('jour4', 'jour4_54-57-Brossay-Becon')
mergeTracks('jour5', 'jour5_60-63-Becon-Nantes')
mergeTracks('jour6', 'jour6_64-66-Nantes-StNazaire-viaPontStNazaire')

'FIN'
