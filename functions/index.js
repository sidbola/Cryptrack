const functions = require('firebase-functions');

const admin = require('firebase-admin')
var firebase = admin.initializeApp(functions.config().firebase)
const requestPro = require('request-promise')
const simpleDataUrl = "https://min-api.cryptocompare.com/data/all/coinlist"
const priceMultiFullUrl = "https://min-api.cryptocompare.com/data/pricemultifull"
const tsyms = "USD"

var coinList = []
var apiRequestPromises = []
var historicalDataApiUrls = []
var simpleCoinData

exports.fetchData = functions.https.onRequest((request, responseServer) => {
    requestPro({
        url: simpleDataUrl,
        json: true
    }, function (error, response, body){
        if (!error && response.statusCode === 200){
            coinList = []
            apiRequestPromises = []

            console.log("Successfully recieved basic coin data")

            simpleCoinData = body['Data']

            console.log("Generating api call urls")
            generateRequestUrls()

            console.log("Calling detailed coin data apis")
            getFullCoinData().then(function(allCoinData){
                console.log("Recieved detailed coin data")
                for(let i = 0; i < allCoinData.length; i++){
                    for(ticker in allCoinData[i]['RAW']){

                        var mRaw = allCoinData[i]['RAW'][ticker]['USD']['MKTCAP'] * 1.0

                        var currentCoin = new CoinData(
                            ticker,
                            simpleCoinData[ticker]['CoinName'],
                            mRaw,
                            allCoinData[i]['DISPLAY'][ticker]['USD']['MKTCAP'],
                            allCoinData[i]['DISPLAY'][ticker]['USD']['PRICE'],
                            allCoinData[i]['RAW'][ticker]['USD']['CHANGEPCT24HOUR'] * 1.0,
                            allCoinData[i]['DISPLAY'][ticker]['USD']['CHANGEPCT24HOUR'],
                            allCoinData[i]['DISPLAY'][ticker]['USD']['SUPPLY'],
                            allCoinData[i]['DISPLAY'][ticker]['USD']['OPEN24HOUR'],
                            allCoinData[i]['DISPLAY'][ticker]['USD']['LOW24HOUR'],
                            allCoinData[i]['DISPLAY'][ticker]['USD']['HIGH24HOUR'],
                            allCoinData[i]['DISPLAY'][ticker]['USD']['VOLUME24HOUR']
                        )
                        if(currentCoin.ticker != 'BITCNY' &&
                            currentCoin.ticker != 'VERI' &&
                            currentCoin.ticker != 'AMIS' &&
                            currentCoin.ticker != 'ERC20' &&
                            currentCoin.ticker != 'MTN' &&
                            currentCoin.ticker != 'WC' &&
                            currentCoin.ticker != 'ATM*' &&
                            currentCoin.ticker != 'DAR' &&
                            currentCoin.ticker != 'KIN'){
                                coinList.push(currentCoin)
                            }
                    }
                }


                coinList.sort(compareCoinMarketcaps)
                console.log("Sorted coin list")

                while(coinList[0].ticker != "BTC"){
                    console.log("Removed " + coinList[0].name + " from coin list")
                    coinList.shift();
                }

                //admin.database().ref().remove()
                //admin.database().ref().child('allCoins').remove()
                //console.log("Deleted old data")

                let top100 = coinList.slice(0,50)

                admin.database().ref().child('discoverData').update({top100}, function(error){
                    if(error){
                        //responseServer.send("Error writing to database")
                        console.log("Error writing: " + error)
                    } else {
                        //responseServer.send("Successful write to database")
                        console.log("Successful write to database Top 100")
                        //attach24hrDataPoints(top100)
                    }
                });



                for (let i = 0; i < coinList.length; i++){
                    admin.database().ref().child('allCoins')
                    .child(coinList[i].ticker).set(coinList[i])
                }


                let top200 = coinList.slice(0,350)
                top200.sort(highestChangeFirst)
                let topWinners = top200.slice(0,10)
                let topLosers = top200.slice(339,349)

                admin.database().ref().child('discoverData').update({topWinners}, function(error){
                    if(error){
                        //responseServer.send("Error writing to database")
                        console.log("Error writing: " + error)
                    } else {
                        //responseServer.send("Successful write to database")
                        console.log("Successful write to database Top Winners")
                        //attach24hrDataPoints(top100)
                    }
                })

                admin.database().ref().child('discoverData').update({topLosers}, function(error){
                    if(error){
                        //responseServer.send("Error writing to database")
                        console.log("Error writing: " + error)
                    } else {
                        //responseServer.send("Successful write to database")
                        console.log("Successful write to database Top Losers")
                        responseServer.send("Successful write to database")
                        //attach24hrDataPoints(top100)
                    }
                })



            }).catch(function(e){
                console.log('Exception: ' + e)
            });

        }
    });
});


function compareCoinMarketcaps(firstCoin, secondCoin){
    if (firstCoin.marketCapRaw < secondCoin.marketCapRaw){
        return 1;
    }
    if (firstCoin.marketCapRaw > secondCoin.marketCapRaw){
        return -1;
    }
    return 0
}

function highestChangeFirst(firstCoin, secondCoin){
    if (firstCoin.percentChangeRaw < secondCoin.percentChangeRaw){
        return 1;
    }
    if (firstCoin.percentChangeRaw > secondCoin.percentChangeRaw){
        return -1;
    }
    return 0
}



function attach24hrDataPoints(listOfCoins){
    console.log("begin fetching 24h coin data")
    for (let i = 0; i < listOfCoins.length; i++){
        let requestUrl = "https://min-api.cryptocompare.com/data/histohour?fsym=" +
            listOfCoins[i].ticker + "&tsym=USD&limit=24"
        historicalDataApiUrls.push(requestUrl)
    }

    for (let j = 0; j < historicalDataApiUrls.length; j++){
        return new Promise((resolve, reject) => {
            setTimeout(function(){
                requestPro({
                    url: historicalDataApiUrls[j],
                    json: true
                }, function(error, histDataResp, histDataBody){
                    if (!error && histDataResp.statusCode === 200){
                        let dataArray = [0,0,0,0]

                        if(histDataBody['Data'].length > 0){
                            dataArray = []
                            for (let k = 0; k < 24; k++){
                                let num = histDataBody['Data'][k]['close']
                                dataArray.push(num)
                            }
                        } else {
                            console.log("No data for: " + listOfCoins[j].ticker)
                        }

                        admin.database().ref().child('top100')
                            .child(j.toString()).child('dataPoints').set(dataArray)
                    }
                })
            }, 150 * j)
        })

    }
}

function generateRequestUrls(){
    var fsyms = ""
    var symCounter = 0

    for (ticker in simpleCoinData){
        fsyms += ticker + ","
        symCounter++

        if (symCounter == 50){
            addToRequestUrlList(fsyms)
            symCounter = 0
            fsyms = ""
        }
    }

    if (symCounter != 0){
        addToRequestUrlList(fsyms)
    }
}

function addToRequestUrlList(fsyms){
    var requestUrl = priceMultiFullUrl + "?fsyms="
     + fsyms + "&tsyms=USD"
    apiRequestPromises.push(requestUrl)
}

function getFullCoinData(){
    let promises = [];
    for (let i = 0; i < apiRequestPromises.length; i++){
        let options = {
            method:'GET',
            json: true,
            uri: apiRequestPromises[i]
        };
        promises.push(requestPro.get(options))
    }
    return Promise.all(promises.map(p => p.catch(e => e)))
}

function CoinData(ticker, name, marketCapRaw, marketCapDisplay, price, percentChangeRaw,
     percentChange, supply, open24hr, low24hr, high24hr, volume24hr){
    this.ticker = ticker;
    this.name = name;
    this.marketCapRaw = marketCapRaw;
    this.marketCapDisplay = marketCapDisplay;
    this.price = price;
    this.percentChangeRaw = percentChangeRaw;
    this.percentChange = percentChange;
    this.supply = supply;
    this.open24hr = open24hr;
    this.low24hr = low24hr;
    this.high24hr = high24hr;
    this.volume24hr = volume24hr;
}
