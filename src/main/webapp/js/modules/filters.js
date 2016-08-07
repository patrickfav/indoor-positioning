(function() {
	'use strict';

	angular.module('sensor-ng-filter', [])
	.filter('cut', function () {
		return function (value, wordwise, max, tail) {
			if (!value) return '';

			max = parseInt(max, 10);
			if (!max) return value;
			if (value.length <= max) return value;

			value = value.substr(0, max);
			if (wordwise) {
				var lastspace = value.lastIndexOf(' ');
				if (lastspace != -1) {
					value = value.substr(0, lastspace);
				}
			}

			return value + (tail || ' ...');
		};
	})
	.filter('maxAge', function () {
		return function (value, wordwise, max, tail) {
			if (!value) return '';

			if (value < 0) {
				return "default";
			} else if (value > 0 && value < 60) {
				return value + " s";
			} else if (value >= 60 && value < 3600) {
				return value / 60 + " min";
			} else if (value >= 3600) {
				return value / 3600 + " h";
			}

			return value;
		};
	})
	.filter('nullCheck', function () {
		return function (value) {
			if (!value || typeof value == "undefiend") return 'no';
			return 'yes';
		};
	}).filter('nullRepresentation', function () {
			return function (value,nullValue) {
				if (!value || typeof value == "undefiend") return nullValue;
				return value;
			};
	})
	.filter('zeroPad', function () {
		return function (value, zeroPaddingCount) {
			if (value != 0 && !value) return '';

			var num = parseInt(value);
			if (num != 0 && !num) return value;
			return ('000000000' + num).substr(-zeroPaddingCount)
		};
	})
	.filter('fileSize', function () {
		return function (byteValue) {
			if (!byteValue) return '';

			var num = parseInt(byteValue);
			if (!num) return byteValue;

			if (byteValue < 1024) {
				return byteValue + " byte";
			} else if (byteValue < 1024 * 1024) {
				return (byteValue / 1024).toFixed(2) + " kb";
			} else if (byteValue < 1024 * 1024 * 1024) {
				return (byteValue / (1024 * 1024)).toFixed(2) + " Mb";
			} else {
				return (byteValue / (1024 * 1024 * 1024)).toFixed(2) + " Gb";
			}
		};
	})
	.filter('dateRange', function ($filter) {
		return function (from, to, formatFull, formatSameDay) {
			var format1 = formatFull ? formatFull : 'M/d/yy h:mm a';
			var format2 = formatSameDay ? formatSameDay : 'h:mm a';
			var fromDate = new Date(from);
			var toDate = new Date(to);

			if (fromDate.toLocaleDateString() == toDate.toLocaleDateString()) {
				return $filter('date')(fromDate, format1) + " - " + $filter('date')(toDate, format2);
			} else {
				return $filter('date')(fromDate, format1) + " - " + $filter('date')(toDate, format1);
			}
		};
	})
	.filter('toArray', function () {
		return function (obj) {
			if (!(obj instanceof Object)) {return obj;}
			return Object.keys(obj).map(function (key) {
				return Object.defineProperty(obj[key], '$key', {__proto__: null, value: key});
			});
		}});
})();