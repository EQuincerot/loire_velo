import groovy.util.XmlSlurper
import groovy.transform.Field
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    String namespace = 'http://www.topografix.com/GPX/1/1'
    if (parsedXml.declareNamespace(x: 'http://www.topografix.com/GPX/1/0').'x:trk'.'x:trkseg'.'x:trkpt'.size()) {
        namespace = 'http://www.topografix.com/GPX/1/0'
    }
    return namespace
}

enum GpxType {
    TRK(['trk', 'trkseg'], 'trkpt'), RTE(['rte'], 'rtept');

    private List<String> rootPath
    String pointName

    GpxType(List<String> rootPath, String pointName) {
        this.rootPath = rootPath
        this.pointName = pointName
    }

    def extractPathFromXml(def xml) {
        def result = xml
        rootPath.each {
            result = result."x:$it"
        }
        return result
    }
}


/**
 * Copy the points in the file into out
 * @param out where to copy the points
 * @param file input
 * @return total processed points
 */
int copyPointsInFile(def out, def file, LocalDateTime time) {

    out.println "<!-- $file -->"
    
    def parsedXml = new XmlSlurper().parseText(file.text)

    String namespace = guessNamespace(parsedXml)
    def parsedXmlWithNs = parsedXml.declareNamespace(x: namespace)

    GpxType gpxType = GpxType.TRK
    int trackCount = gpxType.extractPathFromXml(parsedXmlWithNs).size()
    if (trackCount == 0) {
        gpxType = GpxType.RTE
        trackCount = gpxType.extractPathFromXml(parsedXmlWithNs).size()
    }

    if (trackCount != 1) {
        throw new RuntimeException("Found $trackCount tracks. One expected")
    }

    boolean reverse = filesToReverse.find { rule -> file.name.contains(rule) }
    
    int totalPoint = 0
    int totalElevationFound = 0
    String copy = ''
    gpxType.extractPathFromXml(parsedXmlWithNs)."x:${gpxType.pointName}"
            .each { point ->

        String elevation = point.ele.text()
        if (elevation) {
            elevation="<ele>${elevation}</ele>"
            totalElevationFound++
        }
        // We need a time to be able to import the trace into openstreetmap
        String timeTag = "<time>${time.format(DateTimeFormatter.ISO_DATE_TIME)}</time>"
        time = time.plusSeconds(1)
            
        String lineForTrackPoint = """<trkpt lat="${point.'@lat'}" lon="${point.'@lon'}">${elevation}${timeTag}</trkpt>\n"""
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
        LocalDateTime time = LocalDateTime.of(2017,01,01,0,0)
        inputFiles.sort { it.name }.each {
            print "\tCopie de ${it.name}"
            totalPoints += copyPointsInFile(out, it, time)
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

    String outputFileName = "$rootPath/generated/${newTrackName}.gpx"
    new File(outputFileName).withWriter { out ->
        println "${outputFileName} :"
        writeFile(out, outputFileName, files)
    }
}

filesToReverse.add('62_Champtoceaux_MauvessurLoire_parcours')

mergeTracks('jour1', 'jour1_42-46_Aubrais-Onzain-via-Chambord')
mergeTracks('jour1-alternatif', 'jour1_Aubrais-Onzain_alt')
mergeTracks('jour2', 'jour2_47-49_Onzain-Tours')
mergeTracks('jour3', 'jour3_50-53_Tours-Brossay-via-Saumur')
mergeTracks('jour4', 'jour4_54-57-Brossay-Becon')
mergeTracks('jour5', 'jour5_60-63-Becon-Nantes')
mergeTracks('jour6', 'jour6_64-66-Nantes-StNazaire-viaPontStNazaire')

'FIN'
