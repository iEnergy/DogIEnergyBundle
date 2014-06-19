# DogIEnergyBundle

To use this bundle we need a working installation of dog http://dog-gateway.github.io/, and install this bundle
Dog dependency bundle's: 

* spchain - stream processor
* xively/cosmoutlet
* Network driver (example modbus knx etc)

The configuration file of bundle is it.proximacentauri.ienergy.osgi.config, which:

    # mapping between dog devices and measure
    source.mapping=sourceDeviceMapping.xml
    #source drain remove pattern (reg expression)
    source.removePattern=-raw
    db.driver=org.postgresql.Driver
    db.url=jdbc:postgresql://localhost:5432/ienergy
    db.username=ienergy
    db.password=ienergy
    db.maxActive=10

Where

* source.mapping - the source file mapping of drain name and dog devices 
* source.removePattern - it's the remove pattern of low level measure before enter in spchain, it's necessary to maintain the association between low level and output of spchain 
	Example: drain power name before spchain is M1_P1_RAW, after only M1_P1
* db.X - database parameter

The second configuration file it's about it.polito.elite.dog.addons.xively.client.cfg, bundle used for comunication between dog and ienergy

    # ----------- COSMOutlet configuration -------------------
    # the API key, only needed for the real COSM web site
    # cosm.Key = 
    # the media type to be delivered, either application/xml or application/json
    cosm.mediaType = application/json
    # The COSM base datastream uri for measures (must end with a trailing /)
    cosm.events.feedURL = http://localhost:8080/JeerpDa/processing/
    # The default feed id for measures (no trailing /)
    cosm.events.default = 106199
    # The COSM base datastream uri for alerts (must end with a trailing /)
    cosm.alerts.feedURL = http://localhost:8080/IEnergyDa/alerts/
    # The default feed id for alerts (no trailing /)
    cosm.alerts.default = 106199
    # The COSM waiting list size (number of alerts/events sent in the same request, up to 500)
    cosm.waitingList.size = 1
    # The delivery queue maximum size
    # if set to 0 the queue has no limit (be aware of possible out-of-memory errors)
    cosm.deliveryQueue.size = 100
    # The waiting list self-tune flag, 
    # if true the delivery queue automatically changes the size of the delivered JSON array
    # to avoid (or at least limit) event dropping
    cosm.deliveryQueue.selfTune = true

The database must be create using the following structure https://github.com/iEnergy/IEnergyDa/blob/master/ddl/ienergy_da.sql

## Requirements 

 * Server Linux debian-like
* Java 1.7+
* Dog 2.5+
* Postgres 9+

## Developement 

The bundle is realized with Eclipse for Plug-in Developement 