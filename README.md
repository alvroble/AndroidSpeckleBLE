# AndroidSpeckleBLE
Android Bluetooth Low Energy (BLE) client for communicating with wearable heart rate sensor powered by Raspberry Pi Zero W using UART Service. First, this application begins to search for our device, using the excellent painkiller [Scanner Compat library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/) by NordicSemiconductor, and applying some filters to refine the search. Then, when a device is connected, we'll start an UART communication with it, so we can START and STOP the heart-rate monitoring. The heart-rate values are plotted into a graph, using [GraphView](hhttps://github.com/jjoe64/GraphView/) by jjoe64.

## Prerequisites

This app asks for Bluetooth and Location permissions, and will work fine from API 23 on.


## Built With

* [Scanner Compat library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/)
* [GraphView](https://github.com/jjoe64/GraphView/)
* [Butter Knife](http://jakewharton.github.io/butterknife/)

## Authors

* **Alvaro Robledo** 
* **Ignacio Sánchez**

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Thanks to [jjoe64](https://github.com/jjoe64/) for the Graphview development.
* Thanks to [NordicSemiconductor](https://github.com/NordicSemiconductor/) for the Scanner Compat library and for the UART Service development.
* Thanks to [jakewharton](http://jakewharton.github.io/butterknife/) for the Butter Knife development.
