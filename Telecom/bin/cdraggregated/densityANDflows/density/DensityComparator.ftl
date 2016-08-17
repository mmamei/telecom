
<#setting locale="en_US">

We compare density for ${kop} location in ${region} obtained from a sample of ${limit} individuals with groundtruth information from ISTAT Census in 2011.
Figure \ref{${img}} shows results of the correlation<#if (log)> (results are depicted in log-log scale)</#if>. Resulting $r^2$ is ${r2}.



\begin{figure}
\begin{center}
\includegraphics[width=0.45\columnwidth]{${img}}
\end{center}
\caption{Correlation between density for ${kop} location in ${region} from CDR data and ISTAT Census in 2011}
\label{${img}}
\end{figure}