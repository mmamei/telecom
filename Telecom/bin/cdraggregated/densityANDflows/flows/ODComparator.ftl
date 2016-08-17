
<#-- 
Metodo=HW, 
Istante di inizio=01-03-2014 0, 
Zona interessata=odlombardia, 
Soglia per italiani=8, 
name=HW-odlombardia-01-03-2014-0-8-1-20000-Tutti-30-03-2014-1-8, 
Tipologia utenti=Tutti, # di utenti su utenti del campione=20000, 
Applicato filtro privacy=1, 
img=/img/od/ODMatrixHW-file-pls-lomb-file-pls-lomb-01-03-2014-30-03-2014-minH-0-maxH-25-ABOVE-8limit-20000-cellXHour-odlombardiaCompare2-ISTAT-Lombardia-1.pdf, 
Istante di fine=30-03-2014 1, 
r2=0.7536943317139826, 
log=true,
scale=true,
istatH=3,
reigon=Lombardia,
Soglia per stranieri=8} 
-->



<#setting locale="en_US">


In this experiment we compare the <#if (scale)>scaled</#if> OD-matrix computed with the ${Metodo} method in region ${region}. OD matrix is computed at ${istatH} time interval and compared 
with the corresponding ISTAT information.
Figure \ref{${img}} shows correlation results <#if (log)> (depicted in log-log scale)</#if>. 
Resulting $r^2$ is ${r2}.

\begin{figure}
\begin{center}
\includegraphics[width=0.45\columnwidth]{${img}}
\end{center}
\caption{Correlation od matrix}
\label{${img}}
\end{figure}