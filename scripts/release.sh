#!/bin/bash

rm -rf ./public/js/*
npx shadow-cljs release frontend

cp -R ./public ~/code/upgradingdave/firebase/public/chess-vision
