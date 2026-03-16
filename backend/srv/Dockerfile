# Stage 1: Build React
FROM node:20 AS web-build
WORKDIR /web
COPY ../../Application-web/package*.json ./
RUN npm install
COPY ../../Application-web/ .
RUN npm run build

# Stage 2: Build C#
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY ["srv.csproj", "."]
RUN dotnet restore "./srv.csproj"
COPY . .
RUN dotnet build "./srv.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "./srv.csproj" -c Release -o /app/publish /p:UseAppHost=false

# Stage 3: Final image
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS final
WORKDIR /app
COPY --from=publish /app/publish .
# Copy React build into wwwroot so ASP.NET serves it
COPY --from=web-build /web/dist ./wwwroot

ENTRYPOINT ["dotnet", "srv.dll"]