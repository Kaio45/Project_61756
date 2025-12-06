@echo off
title Client Console
echo Starting Client...
java -Djava.library.path="lib" --module-path "lib" --add-modules javafx.controls,javafx.fxml -jar G17_Prototype_Client.jar
pause