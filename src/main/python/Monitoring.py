from pyspark import SparkContext
from pyspark.streaming import StreamingContext
from pprint import pprint

class Monitoring:
    """
        Wrapper for test jobs, allows users forgot about spark startup details,
        for the end users only knowledge about DStream[T] transformation required
    """

    def __init__(self, monitoringPort = 9990, windowDuration = 60):
        self.context = SparkContext("local[*]", "python-monitoring")
        self.streamingContext = StreamingContext(self.context, monitoringDuration)
        self.passedData = self.streamingContext.socketTextStream("localhost", monitoringPort)

    def watch(self, transfromation):
        transfromation(self.passedData)
        self.streamingContext.start()
        self.streamingContext.awaitTermination()

    def endWatch(self):
        self.streamingContext.stop()

#Initialize monitoring
monitoring = Monitoring(windowDuration = 10)

#Start watching stream
def transformation(incomingData):
    transformedResult = incomingData.flatMap(lambda v: v.split("\n")).map(lambda v: int(v)).countByValue()
    def collectValueFunc(chunk):
        mapper = chunk.collectAsMap()
        zeros = mapper.get(0, 0)
        ones = mapper.get(1, 0)
        pprint("zeros/all ratio - {0} / {1}".format(zeros, zeros + ones))
    transformedResult.foreachRDD(collectValueFunc)

monitoring.watch(transformation)

#Stop monitoring
# monitoring.endWatch
