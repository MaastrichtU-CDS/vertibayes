# VertiBayes

This project implements various bayesian network prototols using the n-party scalar project protocol
library ( https://gitlab.com/fvandaalen/n-scalar-product-protocol )

## Project setup:

This project uses Spring boot at its basis. The following properties are needed:

```
servers=<list of urls for the other servers>
secretServer=<url to the commodity server>
datapath=<path to a csv containing local data>
server.port=<port to be used>
server=<server id>
```

## Data setup:

The project currently expects data to be presented in a csv. The top row is assumed to contain the typing of the
attributes (bool, string, number). The second row is assumed to be attribute ID's. We also assume the first collumn to
contain ID's. The assumption is that the first collumn contains the recordId's.

### Unknown data

It is assumed unknown data has the value of '?'

## Implemented methods:

K2: determines network structure Maximum likelhood: Assigns probabilities to the values in nodes based on parent-child
counts (e.g. calculates P(A|B))
Expectation Maximization 2-step approach:

- Step one: Train a network using maximum likelyhood. Then use this initial network to generate synthetic data
- Step two: Train a second network on this synthetic data using EM implemented in WEKA

Both the K2 and Maximum Likelyhood protocols rely on the n-party scalar product protocol

Continuous data can be binned

### Input data:

Input is provided using CSV-files. Collumns in these CSV files represent attributes and have the following structure:

- First row: indicates attribute type (string, numeric, real, bool)
- Second row: attribute name
- n'th rows: attribute value for the n'th individual

### Request example Expectation Maximization

The request for Maximum Likelyhood is similar except it does not contain a target.

```
{
  "nodes" : [ {
    "parents" : [ ],
    "name" : "x1",
    "type" : "numeric",
    "probabilities" : [ ],
    "bins" : [ {
      "upperLimit" : "1.5",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-1"
    }, {
      "upperLimit" : "?",
      "lowerLimit" : "?"
    } ],
    "discrete" : true
  }, {
    "parents" : [ "x1" ],
    "name" : "x2",
    "type" : "real",
    "probabilities" : [ ],
    "bins" : [ {
      "upperLimit" : "1.5",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-1"
    }, {
      "upperLimit" : "?",
      "lowerLimit" : "?"
    } ],
    "discrete" : false
  }, {
    "parents" : [ "x2","x1" ],
    "name" : "x3",
    "type" : "string",
    "probabilities" : [ ],
    "bins" : [ ],
    "discrete" : true
  } ],
  "target" : "x3"
}
```

### Response example:

```
{
  "nodes" : [ {
    "parents" : [ ],
    "name" : "x1",
    "type" : "real",
    "probabilities" : [ {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "0.5",
        "lowerLimit" : "-inf",
        "range" : true
      },
      "parentValues" : [ ],
      "p" : 0.4010119726339795
    }, {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "inf",
        "lowerLimit" : "0.5",
        "range" : true
      },
      "parentValues" : [ ],
      "p" : 0.5989880273660205
    } ],
    "bins" : [ {
      "upperLimit" : "inf",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-inf"
    } ]
  }, {
    "parents" : [ "x1" ],
    "name" : "x2",
    "type" : "real",
    "probabilities" : [ {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "0.5",
        "lowerLimit" : "-inf",
        "range" : true
      },
      "parentValues" : [ {
        "parent" : "x1",
        "value" : {
          "localValue" : null,
          "upperLimit" : "0.5",
          "lowerLimit" : "-inf",
          "range" : true
        }
      } ],
      "p" : 0.9987562189054726
    }, {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "inf",
        "lowerLimit" : "0.5",
        "range" : true
      },
      "parentValues" : [ {
        "parent" : "x1",
        "value" : {
          "localValue" : null,
          "upperLimit" : "0.5",
          "lowerLimit" : "-inf",
          "range" : true
        }
      } ],
      "p" : 0.0012437810945273632
    }, {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "0.5",
        "lowerLimit" : "-inf",
        "range" : true
      },
      "parentValues" : [ {
        "parent" : "x1",
        "value" : {
          "localValue" : null,
          "upperLimit" : "inf",
          "lowerLimit" : "0.5",
          "range" : true
        }
      } ],
      "p" : 0.5323578396383536
    }, {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "inf",
        "lowerLimit" : "0.5",
        "range" : true
      },
      "parentValues" : [ {
        "parent" : "x1",
        "value" : {
          "localValue" : null,
          "upperLimit" : "inf",
          "lowerLimit" : "0.5",
          "range" : true
        }
      } ],
      "p" : 0.46764216036164646
    } ],
    "bins" : [ {
      "upperLimit" : "inf",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-inf"
    } ]
  }, {
    "parents" : [ "x2" ],
    "name" : "x3",
    "type" : null,
    "probabilities" : [ {
      "localValue" : {
        "localValue" : "0",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "x2",
        "value" : {
          "localValue" : null,
          "upperLimit" : "0.5",
          "lowerLimit" : "-inf",
          "range" : true
        }
      } ],
      "p" : 0.3986727416798732
    }, {
      "localValue" : {
        "localValue" : "1",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "x2",
        "value" : {
          "localValue" : null,
          "upperLimit" : "0.5",
          "lowerLimit" : "-inf",
          "range" : true
        }
      } ],
      "p" : 0.6013272583201268
    }, {
      "localValue" : {
        "localValue" : "0",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "x2",
        "value" : {
          "localValue" : null,
          "upperLimit" : "inf",
          "lowerLimit" : "0.5",
          "range" : true
        }
      } ],
      "p" : 0.002793296089385475
    }, {
      "localValue" : {
        "localValue" : "1",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "x2",
        "value" : {
          "localValue" : null,
          "upperLimit" : "inf",
          "lowerLimit" : "0.5",
          "range" : true
        }
      } ],
      "p" : 0.9972067039106145
    } ],
    "bins" : [ ]
  } ]
}
```
