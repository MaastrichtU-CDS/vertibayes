# VertiBayes

This project implements the K2 algorithm using the n-party scalar project protocol
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

## ToDo:

Implement other aspects of graphical model learning (e.g. EM)
