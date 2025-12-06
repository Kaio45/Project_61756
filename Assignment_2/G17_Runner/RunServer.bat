@echo off
title Server Console
echo Starting Server...
java -Djava.library.path="lib" --module-path "lib" --add-modules javafx.controls,javafx.fxml -jar G17_Prototype_Server.jar
pause