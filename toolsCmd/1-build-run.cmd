@rem product mode
call 0-make-jar.cmd
set CUST_ERR=%ERRORLEVEL%
echo cust_err %CUST_ERR%
@pushd ..
@IF %CUST_ERR% GTR 0 goto L_ERR_1
setlocal
echo running ...
set remote_debug=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:46753
java %remote_debug% -Djava.io.tmpdir=tmpFolder -Dlog4j2.configurationFile=config/log4j2.xml -Dvar123=viaCmdBuildRun -jar build\libs\251_CodeSnippets.jar
endlocal
goto L_END

:L_ERR_1
echo compile error. need fixup
goto L_END

:L_END
@popd